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
    this.actorCod = this.route.snapshot.paramMap.get('actorCod') || '';
    await this.loadVideos('recent');
  }

  async loadVideos(sort: string): Promise<void> {
    this.sortMode = sort;
    const rpt = await this.publicVideoService.findByActor(this.actorCod, sort);
    this.videos = rpt.Data || [];
    this.actor = rpt.DataAdditional?.Actor || new ActorEntity();
  }

  thumb(video: VideoCardDto): string {
    return video.ThumbnailUrl || 'assets/default-video.svg';
  }
}
