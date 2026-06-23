import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { PublichomeComponent } from './enterprise/video/pages/publichome/publichome.component';
import { PubliccategoryComponent } from './enterprise/video/pages/publiccategory/publiccategory.component';
import { PublicactorComponent } from './enterprise/video/pages/publicactor/publicactor.component';
import { PublicplayerComponent } from './enterprise/video/pages/publicplayer/publicplayer.component';
import { LoginComponent } from './enterprise/admin/pages/login/login.component';
import { DashboardComponent } from './enterprise/admin/pages/dashboard/dashboard.component';
import { ListvideosComponent } from './enterprise/admin/pages/listvideos/listvideos.component';
import { CreatevideoComponent } from './enterprise/admin/pages/createvideo/createvideo.component';
import { ListcatalogComponent } from './enterprise/admin/pages/listcatalog/listcatalog.component';

@NgModule({
  declarations: [
    AppComponent,
    PublichomeComponent,
    PubliccategoryComponent,
    PublicactorComponent,
    PublicplayerComponent,
    LoginComponent,
    DashboardComponent,
    ListvideosComponent,
    CreatevideoComponent,
    ListcatalogComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {}
