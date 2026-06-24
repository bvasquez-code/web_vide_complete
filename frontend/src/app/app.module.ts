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
import { PubliccategoriesComponent } from './enterprise/video/pages/publiccategories/publiccategories.component';
import { PublicplayerComponent } from './enterprise/video/pages/publicplayer/publicplayer.component';
import { PublicloginComponent } from './enterprise/video/pages/publiclogin/publiclogin.component';
import { PublicprofileComponent } from './enterprise/video/pages/publicprofile/publicprofile.component';
import { VideocardComponent } from './enterprise/video/component/videocard/videocard.component';
import { PublictoolbarComponent } from './enterprise/video/component/publictoolbar/publictoolbar.component';
import { BackbuttonComponent } from './enterprise/shared/component/backbutton/backbutton.component';
import { LoginComponent } from './enterprise/admin/pages/login/login.component';
import { DashboardComponent } from './enterprise/admin/pages/dashboard/dashboard.component';
import { ListvideosComponent } from './enterprise/admin/pages/listvideos/listvideos.component';
import { CreatevideoComponent } from './enterprise/admin/pages/createvideo/createvideo.component';
import { ListcatalogComponent } from './enterprise/admin/pages/listcatalog/listcatalog.component';
import { CapturesuggestionsComponent } from './enterprise/admin/pages/capturesuggestions/capturesuggestions.component';
import { VideostatisticsdetailComponent } from './enterprise/admin/component/videostatisticsdetail/videostatisticsdetail.component';
import { ActorstatisticsdetailComponent } from './enterprise/admin/component/actorstatisticsdetail/actorstatisticsdetail.component';
import { StatisticsvideosComponent } from './enterprise/admin/pages/statisticsvideos/statisticsvideos.component';
import { StatisticsactorsComponent } from './enterprise/admin/pages/statisticsactors/statisticsactors.component';
import { StatisticsvideodetailComponent } from './enterprise/admin/pages/statisticsvideodetail/statisticsvideodetail.component';
import { StatisticsactordetailComponent } from './enterprise/admin/pages/statisticsactordetail/statisticsactordetail.component';

@NgModule({
  declarations: [
    AppComponent,
    PublichomeComponent,
    PubliccategoriesComponent,
    PubliccategoryComponent,
    PublicactorComponent,
    PublicplayerComponent,
    PublicloginComponent,
    PublicprofileComponent,
    VideocardComponent,
    PublictoolbarComponent,
    BackbuttonComponent,
    LoginComponent,
    DashboardComponent,
    ListvideosComponent,
    CreatevideoComponent,
    ListcatalogComponent,
    CapturesuggestionsComponent,
    VideostatisticsdetailComponent,
    ActorstatisticsdetailComponent,
    StatisticsvideosComponent,
    StatisticsactorsComponent,
    StatisticsvideodetailComponent,
    StatisticsactordetailComponent
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
