import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PublicVideoService } from '../../service/PublicVideoService';
import { PublicPreferenceService } from '../../service/PublicPreferenceService';
import { VideoCardDto } from '../../model/dto/VideoCardDto';
import { VideoCategoryEntity } from '../../model/entity/VideoCategoryEntity';
import { PaginationDto } from '../../../shared/model/dto/PaginationDto';

@Component({
  selector: 'app-publiccategory',
  templateUrl: './publiccategory.component.html'
})
export class PubliccategoryComponent implements OnInit {
  categoryCod = '';
  sortMode = 'recent';
  category: VideoCategoryEntity = new VideoCategoryEntity();
  videos: VideoCardDto[] = [];
  page = 1;
  limit = 24;
  totalRows = 0;
  pagination = new PaginationDto({ Limit: this.limit, ItemLabel: 'videos' });

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService, public publicPreferenceService: PublicPreferenceService) {}

  async ngOnInit(): Promise<void> {
    this.route.paramMap.subscribe(async params => {
      const nextCategoryCod = params.get('categoryCod') || '';
      if (!nextCategoryCod) {
        return;
      }
      this.categoryCod = nextCategoryCod;
      await this.loadVideos('recent', 1);
    });
  }

  async loadVideos(sort: string, page: number = 1): Promise<void> {
    this.sortMode = sort;
    this.page = page < 1 ? 1 : page;
    const rpt = await this.publicVideoService.findByCategory(this.categoryCod, sort, this.page, this.limit);
    this.videos = rpt.Data?.Data || [];
    this.totalRows = rpt.Data?.TotalRows || 0;
    this.page = rpt.Data?.Page || this.page;
    this.limit = rpt.Data?.Limit || this.limit;
    this.pagination = new PaginationDto({ Page: this.page, Limit: this.limit, TotalRows: this.totalRows, ItemLabel: 'videos' });
    this.category = rpt.DataAdditional?.Category || new VideoCategoryEntity();
  }

  thumb(video: VideoCardDto): string {
    return video.ThumbnailUrl || 'assets/default-video.svg';
  }
}
