import argparse
import json
from pathlib import Path
from datetime import datetime


EXTENSIONES_VIDEO = {
    ".mp4",
    ".avi",
    ".mkv",
    ".mov",
    ".wmv",
    ".flv",
    ".webm",
    ".m4v"
}


def generar_lista_videos(ruta_entrada: str) -> list[str]:
    carpeta_entrada = Path(ruta_entrada)

    if not carpeta_entrada.exists():
        raise FileNotFoundError(f"La ruta de entrada no existe: {ruta_entrada}")

    if not carpeta_entrada.is_dir():
        raise NotADirectoryError(f"La ruta de entrada no es una carpeta: {ruta_entrada}")

    lista_videos = []

    for archivo in carpeta_entrada.rglob("*"):
        if archivo.is_file() and archivo.suffix.lower() in EXTENSIONES_VIDEO:
            ruta_normalizada = str(archivo.resolve()).replace("\\", "/")
            lista_videos.append(ruta_normalizada)

    return lista_videos


def guardar_json(lista_videos: list[str], ruta_salida: str, nombre_json: str) -> Path:
    carpeta_salida = Path(ruta_salida)

    if not carpeta_salida.exists():
        carpeta_salida.mkdir(parents=True, exist_ok=True)

    fecha_correlativo = datetime.now().strftime("%Y%m%d_%H%M%S")

    nombre_limpio = Path(nombre_json).stem
    nombre_archivo = f"{nombre_limpio}_{fecha_correlativo}.json"

    ruta_json = carpeta_salida / nombre_archivo

    resultado = {
        "lista_video": lista_videos
    }

    with open(ruta_json, "w", encoding="utf-8") as archivo:
        json.dump(resultado, archivo, ensure_ascii=False, indent=4)

    return ruta_json


def main():
    parser = argparse.ArgumentParser(
        description="Genera un JSON con la lista de videos encontrados en una carpeta y subcarpetas."
    )

    parser.add_argument(
        "--entrada",
        required=True,
        help="Ruta de la carpeta donde se buscarán los videos."
    )

    parser.add_argument(
        "--salida",
        required=True,
        help="Ruta de la carpeta donde se guardará el JSON generado."
    )

    parser.add_argument(
        "--nombre",
        required=True,
        help="Nombre base del archivo JSON resultante. Ejemplo: lista_videos"
    )

    args = parser.parse_args()

    lista_videos = generar_lista_videos(args.entrada)
    ruta_json = guardar_json(lista_videos, args.salida, args.nombre)

    print(f"JSON generado correctamente:")
    print(str(ruta_json).replace("\\", "/"))
    print(f"Cantidad de videos encontrados: {len(lista_videos)}")


if __name__ == "__main__":
    main()