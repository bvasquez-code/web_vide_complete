export class VideoEntity {
  VideoCod: string = '';
  Title: string = '';
  ShortDescription: string = '';
  LongDescription: string = '';
  ThumbnailUrl: string = '';
  SourceType: string = 'URL';
  SourceValue: string = '';
  Duration: string = '';
  FileSizeBytes: number = 0;
  ResolutionWidth: number = 0;
  ResolutionHeight: number = 0;
  ViewCount: number = 0;
  PublishDate: string = '';
  Status: string = 'A';
}
