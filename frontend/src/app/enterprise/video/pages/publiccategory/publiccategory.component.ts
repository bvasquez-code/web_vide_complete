import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PublicVideoService } from '../../service/PublicVideoService';
import { VideoCardDto } from '../../model/dto/VideoCardDto';
import { VideoCategoryEntity } from '../../model/entity/VideoCategoryEntity';

@Component({
  selector: 'app-publiccategory',
  templateUrl: './publiccategory.component.html'
})
export class PubliccategoryComponent implements OnInit {
  categoryCod = '';
  sortMode = 'recent';
  category: VideoCategoryEntity = new VideoCategoryEntity();
  videos: VideoCardDto[] = [];

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService) {}

  async ngOnInit(): Promise<void> {
    this.categoryCod = this.route.snapshot.paramMap.get('categoryCod') || '';
    await this.loadVideos('recent');
  }

  async loadVideos(sort: string): Promise<void> {
    this.sortMode = sort;
    const rpt = await this.publicVideoService.findByCategory(this.categoryCod, sort);
    this.videos = rpt.Data || [];
    this.category = rpt.DataAdditional?.Category || new VideoCategoryEntity();
  }

  thumb(video: VideoCardDto): string {
    return video.ThumbnailUrl || 'assets/default-video.svg';
  }
}
