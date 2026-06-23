import { Component, OnInit } from '@angular/core';
import { AdminVideoService } from '../../../video/service/AdminVideoService';
import { VideoEntity } from '../../../video/model/entity/VideoEntity';

@Component({
  selector: 'app-listvideos',
  templateUrl: './listvideos.component.html'
})
export class ListvideosComponent implements OnInit {
  Query = '';
  Status = '';
  SourceType = '';
  videos: VideoEntity[] = [];
  message = '';

  constructor(private adminVideoService: AdminVideoService) {}

  async ngOnInit(): Promise<void> {
    await this.findAll();
  }

  async findAll(): Promise<void> {
    const rpt = await this.adminVideoService.findVideos({ Query: this.Query, Status: this.Status, SourceType: this.SourceType, Page: 1, Limit: 20 });
    this.videos = rpt.Data?.Data || [];
  }

  async enable(video: VideoEntity): Promise<void> {
    await this.adminVideoService.enableVideo(video.VideoCod);
    await this.findAll();
  }

  async disable(video: VideoEntity): Promise<void> {
    await this.adminVideoService.disableVideo(video.VideoCod);
    await this.findAll();
  }
}
