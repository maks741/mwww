import argparse
import yt_dlp
        

base_songs_dir_path = "./songs/"


def download_audio(url):
  with yt_dlp.YoutubeDL({
        'extract_audio': True,
        'writethumbnail': True,
        'format': 'bestaudio',
        'outtmpl': base_songs_dir_path + '%(uploader)s-%(title)s/%(uploader)s-%(title)s',
        'postprocessors': [
            {
                'key': 'FFmpegExtractAudio',
                'preferredcodec': 'wav'
            },
            {
                'key': 'FFmpegThumbnailsConvertor',
                'format': 'jpg',
            }
        ],
        'quiet': True
    }) as video:
    video.download(url)


if __name__ == '__main__':
    arg_parser = argparse.ArgumentParser()
    arg_parser.add_argument("url", help="URL of the youtube video", type=str)

    args = arg_parser.parse_args()

    url = args.url

    download_audio(url)
