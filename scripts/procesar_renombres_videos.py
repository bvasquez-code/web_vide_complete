import argparse
import json
import shutil
from pathlib import Path
from datetime import datetime


def limpiar_nombre_archivo(nombre: str) -> str:
    """
    Limpia caracteres inválidos para nombres de archivo en Windows.
    """
    caracteres_invalidos = ['<', '>', ':', '"', '/', '\\', '|', '?', '*']

    nombre_limpio = nombre.strip()

    for caracter in caracteres_invalidos:
        nombre_limpio = nombre_limpio.replace(caracter, "-")

    return nombre_limpio


def generar_nombre_disponible(ruta_destino: Path) -> Path:
    """
    Si el archivo ya existe, genera un nombre con correlativo.
    Ejemplo:
    pelicula.mp4
    pelicula_001.mp4
    pelicula_002.mp4
    """
    if not ruta_destino.exists():
        return ruta_destino

    carpeta = ruta_destino.parent
    nombre_base = ruta_destino.stem
    extension = ruta_destino.suffix

    contador = 1

    while True:
        nuevo_nombre = f"{nombre_base}_{contador:03d}{extension}"
        nueva_ruta = carpeta / nuevo_nombre

        if not nueva_ruta.exists():
            return nueva_ruta

        contador += 1


def procesar_json(ruta_json: str):
    archivo_json = Path(ruta_json)

    if not archivo_json.exists():
        raise FileNotFoundError(f"No existe el archivo JSON: {ruta_json}")

    with open(archivo_json, "r", encoding="utf-8") as archivo:
        data = json.load(archivo)

    lista_corregida = data.get("lista_corregida", [])

    if not isinstance(lista_corregida, list):
        raise ValueError("El JSON no tiene una lista válida en la propiedad 'lista_corregida'.")

    resultado_proceso = {
        "fecha_proceso": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "total_registros": len(lista_corregida),
        "procesados": [],
        "errores": []
    }

    for index, item in enumerate(lista_corregida, start=1):
        pelicula = item.get("pelicula")
        nombre_recomendado = item.get("nombreRecomendado")

        if not pelicula or not nombre_recomendado:
            resultado_proceso["errores"].append({
                "index": index,
                "pelicula": pelicula,
                "nombreRecomendado": nombre_recomendado,
                "error": "Registro incompleto. Se requiere 'pelicula' y 'nombreRecomendado'."
            })
            continue

        ruta_origen = Path(pelicula)

        if not ruta_origen.exists():
            resultado_proceso["errores"].append({
                "index": index,
                "pelicula": pelicula,
                "nombreRecomendado": nombre_recomendado,
                "error": "El archivo original no existe."
            })
            continue

        if not ruta_origen.is_file():
            resultado_proceso["errores"].append({
                "index": index,
                "pelicula": pelicula,
                "nombreRecomendado": nombre_recomendado,
                "error": "La ruta original no corresponde a un archivo."
            })
            continue

        try:
            carpeta_original = ruta_origen.parent
            carpeta_procesados = carpeta_original / "procesados"
            carpeta_procesados.mkdir(parents=True, exist_ok=True)

            nombre_limpio = limpiar_nombre_archivo(nombre_recomendado)

            ruta_destino = carpeta_procesados / nombre_limpio
            ruta_destino = generar_nombre_disponible(ruta_destino)

            shutil.move(str(ruta_origen), str(ruta_destino))

            resultado_proceso["procesados"].append({
                "index": index,
                "origen": str(ruta_origen).replace("\\", "/"),
                "destino": str(ruta_destino).replace("\\", "/"),
                "estado": "OK"
            })

        except Exception as error:
            resultado_proceso["errores"].append({
                "index": index,
                "pelicula": pelicula,
                "nombreRecomendado": nombre_recomendado,
                "error": str(error)
            })

    return resultado_proceso


def guardar_reporte(resultado_proceso: dict, ruta_json_origen: str) -> Path:
    ruta_json = Path(ruta_json_origen)

    fecha_correlativo = datetime.now().strftime("%Y%m%d_%H%M%S")
    nombre_reporte = f"reporte_renombres_{fecha_correlativo}.json"

    ruta_reporte = ruta_json.parent / nombre_reporte

    with open(ruta_reporte, "w", encoding="utf-8") as archivo:
        json.dump(resultado_proceso, archivo, ensure_ascii=False, indent=4)

    return ruta_reporte


def main():
    parser = argparse.ArgumentParser(
        description="Renombra videos desde un JSON corregido por IA y los mueve a una carpeta 'procesados'."
    )

    parser.add_argument(
        "--json",
        required=True,
        help="Ruta del JSON corregido por IA."
    )

    args = parser.parse_args()

    resultado_proceso = procesar_json(args.json)
    ruta_reporte = guardar_reporte(resultado_proceso, args.json)

    print("Proceso finalizado.")
    print(f"Total registros: {resultado_proceso['total_registros']}")
    print(f"Procesados OK: {len(resultado_proceso['procesados'])}")
    print(f"Errores: {len(resultado_proceso['errores'])}")
    print(f"Reporte generado: {str(ruta_reporte).replace('\\', '/')}")


if __name__ == "__main__":
    main()