import pandas as pd
import numpy as np
import sys
import os
import io
from sklearn.neural_network import MLPClassifier
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.utils import resample

sys.stdout.reconfigure(encoding='utf-8')

def ejecutar_modelo(dia_input, minutos_input, ubicacion_input):
    try:
        base_path = os.path.dirname(os.path.abspath(__file__))
        file_path = os.path.join(base_path, 'entrenamiento_tardanzas.txt')

        with open(file_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()

        data_lines = [l.strip() for l in lines if l and l[0].isdigit()]
        data_str = "\n".join(data_lines)

        df = pd.read_csv(io.StringIO(data_str),
                         names=['dia_semana', 'minutos_retraso', 'referencia_ubicacion', 'resultado_viejo'],
                         quotechar="'",
                         skipinitialspace=True)

        # --- PASO CRÍTICO: LIMPIEZA TOTAL ---
        # Borramos cualquier rastro de la columna 'resultado_viejo' para que no confunda a la IA
        df = df.drop(columns=['resultado_viejo'])

        # Aplicamos la regla que TÚ quieres (La Regla de Oro)
        def aplicar_regla(fila):
            d = int(fila['dia_semana'])
            m = int(fila['minutos_retraso'])
            if d == 2: # Lunes
                return 'TARDE' if m > 10 else 'PUNTUAL'
            else: # Otros días
                return 'TARDE' if m > 15 else 'PUNTUAL'

        df['resultado'] = df.apply(aplicar_regla, axis=1)

        # BALANCEO DE DATOS (Para que aprenda ambos casos por igual)
        df_t = df[df['resultado'] == 'TARDE']
        df_p = df[df['resultado'] == 'PUNTUAL']
        df_p_upsampled = resample(df_p, replace=True, n_samples=len(df_t), random_state=42)
        df_final = pd.concat([df_t, df_p_upsampled])

        X = df_final[['dia_semana', 'minutos_retraso', 'referencia_ubicacion']]
        y = df_final['resultado']

        # PREPROCESADOR
        preprocessor = ColumnTransformer(
            transformers=[
                ('num', StandardScaler(), ['minutos_retraso']),
                ('cat', OneHotEncoder(handle_unknown='ignore'), ['dia_semana', 'referencia_ubicacion'])
            ])

        # RED NEURONAL (Aumentamos max_iter a 2000 para que converja)
        mlp = MLPClassifier(hidden_layer_sizes=(64, 32),
                            max_iter=2000,
                            solver='lbfgs', # Volvemos a lbfgs que es más exacto para reglas fijas
                            random_state=42)

        modelo = Pipeline(steps=[('pre', preprocessor), ('clf', mlp)])
        modelo.fit(X, y)

        # PREDICCIÓN
        nuevo = pd.DataFrame([[dia_input, minutos_input, ubicacion_input.strip()]],
                             columns=['dia_semana', 'minutos_retraso', 'referencia_ubicacion'])

        prediccion = modelo.predict(nuevo)[0]
        probabilidades = modelo.predict_proba(nuevo)[0]
        confianza = max(probabilidades) * 100

        return f"{prediccion},{round(confianza, 2)}"

    except Exception as e:
        return f"Error: {str(e)},0"

if __name__ == "__main__":
    if len(sys.argv) >= 4:
        print(ejecutar_modelo(int(sys.argv[1]), int(sys.argv[2]), sys.argv[3]))