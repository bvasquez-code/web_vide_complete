import { ActorEntity } from '../entity/ActorEntity';
import { TagEntity } from '../entity/TagEntity';
import { VideoCaptureEntity } from '../entity/VideoCaptureEntity';
import { VideoCategoryEntity } from '../entity/VideoCategoryEntity';
import { VideoEntity } from '../entity/VideoEntity';

export class VideoDetailDto {
  Video: VideoEntity = new VideoEntity();
  Categories: VideoCategoryEntity[] = [];
  Actors: ActorEntity[] = [];
  Tags: TagEntity[] = [];
  Captures: VideoCaptureEntity[] = [];
}
