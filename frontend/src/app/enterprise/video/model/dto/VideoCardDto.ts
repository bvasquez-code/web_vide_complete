export class VideoLabelDto {
  Cod: string = '';
  Name: string = '';
}

export class VideoCardDto {
  VideoCod: string = '';
  Title: string = '';
  ShortDescription: string = '';
  ThumbnailUrl: string = '';
  SourceType: string = '';
  Duration: string = '';
  FileSizeBytes: number = 0;
  FileSizeLabel: string = '';
  ResolutionWidth: number = 0;
  ResolutionHeight: number = 0;
  ResolutionLabel: string = '';
  ViewCount: number = 0;
  PublishDate: string = '';
  CreationDate: string = '';
  PrimaryCategoryCod: string = '';
  PrimaryCategoryName: string = '';
  Categories: VideoLabelDto[] = [];
  Actors: VideoLabelDto[] = [];
  Tags: VideoLabelDto[] = [];
}
