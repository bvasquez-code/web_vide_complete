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
  page = 1;
  pageInput = 1;
  limit = 24;
  totalRows = 0;

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService) {}

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
    this.pageInput = this.page;
    this.category = rpt.DataAdditional?.Category || new VideoCategoryEntity();
  }

  totalPages(): number {
    return Math.max(1, Math.ceil(this.totalRows / this.limit));
  }

  canPreviousPage(): boolean {
    return this.page > 1;
  }

  canNextPage(): boolean {
    return this.page < this.totalPages();
  }

  async goToFirstPage(): Promise<void> {
    await this.loadVideos(this.sortMode, 1);
  }

  async goToLastPage(): Promise<void> {
    await this.loadVideos(this.sortMode, this.totalPages());
  }

  async goToPageInput(): Promise<void> {
    const requestedPage = Number(this.pageInput);
    if (!Number.isFinite(requestedPage)) {
      this.pageInput = this.page;
      return;
    }
    const targetPage = Math.trunc(requestedPage);
    if (targetPage < 1 || targetPage > this.totalPages()) {
      this.pageInput = this.page;
      return;
    }
    await this.loadVideos(this.sortMode, targetPage);
  }

  thumb(video: VideoCardDto): string {
    return video.ThumbnailUrl || 'assets/default-video.svg';
  }
}
