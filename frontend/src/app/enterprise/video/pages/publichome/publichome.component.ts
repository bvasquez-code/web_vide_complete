import { Component, OnInit } from '@angular/core';
import { PublicVideoService } from '../../service/PublicVideoService';
import { VideoCategoryEntity } from '../../model/entity/VideoCategoryEntity';
import { VideoCardDto } from '../../model/dto/VideoCardDto';

@Component({
  selector: 'app-publichome',
  templateUrl: './publichome.component.html'
})
export class PublichomeComponent implements OnInit {
  categories: VideoCategoryEntity[] = [];
  videos: VideoCardDto[] = [];
  sortMode = 'recent';
  loading = false;
  errorMessage = '';

  constructor(private publicVideoService: PublicVideoService) {}

  async ngOnInit(): Promise<void> {
    await this.load();
  }

  async load(): Promise<void> {
    this.loading = true;
    this.errorMessage = '';
    const categoryRpt = await this.publicVideoService.findCategories();
    this.categories = categoryRpt.Data || [];
    await this.loadVideos('recent');
    this.loading = false;
  }

  async loadVideos(mode: string): Promise<void> {
    this.sortMode = mode;
    const rpt = mode === 'views' ? await this.publicVideoService.findMostViewed(12) : await this.publicVideoService.findRecent(12);
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    this.videos = rpt.Data || [];
  }

  thumb(video: VideoCardDto): string {
    return video.ThumbnailUrl || 'assets/default-video.svg';
  }
}
