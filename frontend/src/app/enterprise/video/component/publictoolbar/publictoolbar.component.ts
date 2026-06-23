import { Component } from '@angular/core';
import { PublicPreferenceService } from '../../service/PublicPreferenceService';

@Component({
  selector: 'app-publictoolbar',
  templateUrl: './publictoolbar.component.html'
})
export class PublictoolbarComponent {
  constructor(public publicPreferenceService: PublicPreferenceService) {}
}
