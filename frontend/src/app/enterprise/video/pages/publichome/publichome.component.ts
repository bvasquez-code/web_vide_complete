import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
  searchQuery = '';
  searchMode = false;
  searchPage = 1;
  searchLimit = 30;
  searchTotalRows = 0;
  listTitle = 'Videos';
  private initialized = false;

  constructor(private publicVideoService: PublicVideoService, private route: ActivatedRoute) {}

  async ngOnInit(): Promise<void> {
    await this.load();
    this.route.queryParamMap.subscribe(async params => {
      if (!this.initialized) {
        this.initialized = true;
        return;
      }
      const queryParam = params.get('q');
      if (queryParam && queryParam.trim()) {
        this.searchQuery = queryParam.trim();
        await this.search(1);
      } else if (this.searchMode) {
        await this.clearSearch();
      }
    });
  }

  async load(): Promise<void> {
    this.loading = true;
    this.errorMessage = '';
    const categoryRpt = await this.publicVideoService.findTopCategories(8);
    this.categories = categoryRpt.Data || [];
    const queryParam = this.route.snapshot.queryParamMap.get('q');
    if (queryParam && queryParam.trim()) {
      this.searchQuery = queryParam.trim();
      await this.search(1);
    } else {
      await this.loadVideos('recent');
    }
    this.loading = false;
  }

  async loadVideos(mode: string): Promise<void> {
    this.sortMode = mode;
    this.searchPage = 1;
    await this.search(1);
  }

  async search(page: number = 1): Promise<void> {
    this.errorMessage = '';
    this.searchPage = page < 1 ? 1 : page;
    const query = this.searchQuery.trim();
    this.searchMode = !!query;
    this.listTitle = this.searchMode ? 'Resultados de busqueda' : 'Videos';
    const rpt = await this.publicVideoService.search(query, this.searchPage, this.searchLimit, this.sortMode);
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    this.videos = rpt.Data?.Data || [];
    this.searchTotalRows = rpt.Data?.TotalRows || 0;
    this.searchPage = rpt.Data?.Page || this.searchPage;
    this.searchLimit = rpt.Data?.Limit || this.searchLimit;
  }

  async clearSearch(): Promise<void> {
    this.searchQuery = '';
    this.searchMode = false;
    this.searchPage = 1;
    this.searchTotalRows = 0;
    await this.search(1);
  }

  totalSearchPages(): number {
    return Math.max(1, Math.ceil(this.searchTotalRows / this.searchLimit));
  }

  canPreviousSearchPage(): boolean {
    return this.searchPage > 1;
  }

  canNextSearchPage(): boolean {
    return this.searchPage < this.totalSearchPages();
  }

  thumb(video: VideoCardDto): string {
    return video.ThumbnailUrl || 'assets/default-video.svg';
  }
}
