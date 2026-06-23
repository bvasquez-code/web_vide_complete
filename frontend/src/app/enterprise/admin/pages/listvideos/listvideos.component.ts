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
  Page = 1;
  Limit = 20;
  TotalRows = 0;

  constructor(private adminVideoService: AdminVideoService) {}

  async ngOnInit(): Promise<void> {
    await this.findAll(1);
  }

  async findAll(page: number = this.Page): Promise<void> {
    this.Page = page < 1 ? 1 : page;
    const rpt = await this.adminVideoService.findVideos({ Query: this.Query, Status: this.Status, SourceType: this.SourceType, Page: this.Page, Limit: this.Limit });
    this.videos = rpt.Data?.Data || [];
    this.TotalRows = rpt.Data?.TotalRows || 0;
    this.Page = rpt.Data?.Page || this.Page;
    this.Limit = rpt.Data?.Limit || this.Limit;
  }

  async search(): Promise<void> {
    await this.findAll(1);
  }

  totalPages(): number {
    return Math.max(1, Math.ceil(this.TotalRows / this.Limit));
  }

  canPreviousPage(): boolean {
    return this.Page > 1;
  }

  canNextPage(): boolean {
    return this.Page < this.totalPages();
  }

  async enable(video: VideoEntity): Promise<void> {
    await this.adminVideoService.enableVideo(video.VideoCod);
    await this.findAll(this.Page);
  }

  async disable(video: VideoEntity): Promise<void> {
    await this.adminVideoService.disableVideo(video.VideoCod);
    await this.findAll(this.Page);
  }
}
