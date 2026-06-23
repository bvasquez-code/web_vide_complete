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

  constructor(private route: ActivatedRoute, private publicVideoService: PublicVideoService) {}

  async ngOnInit(): Promise<void> {
    this.route.paramMap.subscribe(async params => {
      const nextActorCod = params.get('actorCod') || '';
      if (!nextActorCod) {
        return;
      }
      this.actorCod = nextActorCod;
      await this.loadVideos('recent');
    });
  }

  async loadVideos(sort: string): Promise<void> {
    this.sortMode = sort;
    const rpt = await this.publicVideoService.findByActor(this.actorCod, sort);
    this.videos = rpt.Data || [];
    this.actor = rpt.DataAdditional?.Actor || new ActorEntity();
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
