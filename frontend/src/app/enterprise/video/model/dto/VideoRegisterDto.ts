import { VideoEntity } from '../entity/VideoEntity';

export class VideoRegisterDto {
  Video: VideoEntity = new VideoEntity();
  CategoryCodList: string[] = [];
  ActorCodList: string[] = [];
  TagCodList: string[] = [];
}
