import { Component, OnInit } from '@angular/core';
import { VideoCategoryEntity } from '../../model/entity/VideoCategoryEntity';
import { PublicVideoService } from '../../service/PublicVideoService';
import { PublicPreferenceService } from '../../service/PublicPreferenceService';

@Component({
  selector: 'app-publiccategories',
  templateUrl: './publiccategories.component.html'
})
export class PubliccategoriesComponent implements OnInit {
  categories: VideoCategoryEntity[] = [];

  constructor(private publicVideoService: PublicVideoService, public publicPreferenceService: PublicPreferenceService) {}

  async ngOnInit(): Promise<void> {
    const rpt = await this.publicVideoService.findCategories();
    this.categories = rpt.Data || [];
  }
}
