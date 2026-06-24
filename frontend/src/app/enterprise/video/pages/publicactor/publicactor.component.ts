import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ActorEntity } from '../../model/entity/ActorEntity';
import { VideoCardDto } from '../../model/dto/VideoCardDto';
import { PublicVideoService } from '../../service/PublicVideoService';

@Component({
  selector: 'app-publicactor',
  templateUrl: './publicactor.component.html'
})
export class PublicactorComponent implements OnInit {
  actorCod = '';
  sortMode = 'recent';
  actor: ActorEntity = new ActorEntity();
  videos: VideoCardDto[] = [];
  page = 1;
  pageInput = 1;
  limit = 24;
  totalRows = 0;

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService) {}

  async ngOnInit(): Promise<void> {
    this.route.paramMap.subscribe(async params => {
      const nextActorCod = params.get('actorCod') || '';
      if (!nextActorCod) {
        return;
      }
      this.actorCod = nextActorCod;
      await this.loadVideos('recent', 1);
    });
  }

  async loadVideos(sort: string, page: number = 1): Promise<void> {
    this.sortMode = sort;
    this.page = page < 1 ? 1 : page;
    const rpt = await this.publicVideoService.findByActor(this.actorCod, sort, this.page, this.limit);
    this.videos = rpt.Data?.Data || [];
    this.totalRows = rpt.Data?.TotalRows || 0;
    this.page = rpt.Data?.Page || this.page;
    this.limit = rpt.Data?.Limit || this.limit;
    this.pageInput = this.page;
    this.actor = rpt.DataAdditional?.Actor || new ActorEntity();
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

  actorImage(): string {
    return this.actor.ImageUrl || 'assets/default-actor.svg';
  }

  actorDescription(): string {
    return this.actor.Description || 'Descripcion no registrada.';
  }

  onActorImageError(event: Event): void {
    const image = event.target as HTMLImageElement;
    image.src = 'assets/default-actor.svg';
  }

  thumb(video: VideoCardDto): string {
    return video.ThumbnailUrl || 'assets/default-video.svg';
  }
}
