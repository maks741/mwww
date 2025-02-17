import argparse
import yt_dlp
from PIL import Image
import os
        

base_songs_dir_path = "./songs/"


def crop_image(image):
    width, height = image.size
    if width == height:
        return image
    offset  = int(abs(height-width)/2)
    if width>height:
        image = image.crop([offset,0,width-offset,height])
    else:
        image = image.crop([0,offset,width,height-offset])
    image = image.resize((75, 75))
    return image


def webp_to_jpg(dir_name, thumbnail_webp_name):
    thumbnail_dir = base_songs_dir_path + dir_name + "/"

    webp = Image.open(thumbnail_dir + thumbnail_webp_name)
    jpg = webp.convert("RGBA")
    jpg = crop_image(jpg)
    jpg.save(thumbnail_dir + "thumbnail.png")
    os.remove(thumbnail_dir + thumbnail_webp_name)


def download_audio(url):
  with yt_dlp.YoutubeDL({
        'extract_audio': True,
        'writethumbnail': True,
        'format': 'bestaudio',
        'outtmpl': base_songs_dir_path + '%(uploader)s-%(title)s/%(uploader)s-%(title)s',
        'postprocessors': [{
            'key': 'FFmpegExtractAudio',
            'preferredcodec': 'wav'
        }],
        'quiet': True
    }) as video:
    info_dict = video.extract_info(url)
    title = info_dict["title"]
    uploader = info_dict["uploader"]
    dir_name = uploader + "-" + title
    thumbnail_webp_name = dir_name + ".webp"

    video.download(url)

    webp_to_jpg(dir_name, thumbnail_webp_name)

    print("dirname:" + dir_name)


if __name__ == '__main__':
    arg_parser = argparse.ArgumentParser()
    arg_parser.add_argument("url", help="URL of the youtube video", type=str)

    args = arg_parser.parse_args()

    url = args.url

    download_audio(url)
