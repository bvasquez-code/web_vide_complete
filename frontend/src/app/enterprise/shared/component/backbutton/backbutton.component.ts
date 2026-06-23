import { Component, Input } from '@angular/core';
import { Location } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-backbutton',
  templateUrl: './backbutton.component.html'
})
export class BackbuttonComponent {
  @Input() fallbackUrl = '/';
  @Input() cssClass = 'btn btn-link px-0';

  constructor(private location: Location, private router: Router) {}

  goBack(): void {
    if (window.history.length > 1) {
      this.location.back();
      return;
    }
    this.router.navigateByUrl(this.fallbackUrl);
  }
}
