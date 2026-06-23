import { Component, OnInit } from '@angular/core';
import { VideoCategoryEntity } from '../../model/entity/VideoCategoryEntity';
import { PublicVideoService } from '../../service/PublicVideoService';

@Component({
  selector: 'app-publiccategories',
  templateUrl: './publiccategories.component.html'
})
export class PubliccategoriesComponent implements OnInit {
  categories: VideoCategoryEntity[] = [];

  constructor(private publicVideoService: PublicVideoService) {}

  async ngOnInit(): Promise<void> {
    const rpt = await this.publicVideoService.findCategories();
    this.categories = rpt.Data || [];
  }
}
