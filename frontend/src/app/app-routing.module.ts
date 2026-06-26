import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './enterprise/admin/pages/dashboard/dashboard.component';
import { LoginComponent } from './enterprise/admin/pages/login/login.component';
import { ListcatalogComponent } from './enterprise/admin/pages/listcatalog/listcatalog.component';
import { ListvideosComponent } from './enterprise/admin/pages/listvideos/listvideos.component';
import { CreatevideoComponent } from './enterprise/admin/pages/createvideo/createvideo.component';
import { AdminAuthGuard } from './enterprise/shared/guard/AdminAuthGuard';
import { PubliccategoryComponent } from './enterprise/video/pages/publiccategory/publiccategory.component';
import { PublicactorComponent } from './enterprise/video/pages/publicactor/publicactor.component';
import { PublicactorphotosComponent } from './enterprise/video/pages/publicactorphotos/publicactorphotos.component';
import { PubliccategoriesComponent } from './enterprise/video/pages/publiccategories/publiccategories.component';
import { PublichomeComponent } from './enterprise/video/pages/publichome/publichome.component';
import { PublicplayerComponent } from './enterprise/video/pages/publicplayer/publicplayer.component';
import { PublicloginComponent } from './enterprise/video/pages/publiclogin/publiclogin.component';
import { PublicprofileComponent } from './enterprise/video/pages/publicprofile/publicprofile.component';
import { CapturesuggestionsComponent } from './enterprise/admin/pages/capturesuggestions/capturesuggestions.component';
import { StatisticsvideosComponent } from './enterprise/admin/pages/statisticsvideos/statisticsvideos.component';
import { StatisticsactorsComponent } from './enterprise/admin/pages/statisticsactors/statisticsactors.component';
import { StatisticsvideodetailComponent } from './enterprise/admin/pages/statisticsvideodetail/statisticsvideodetail.component';
import { StatisticsactordetailComponent } from './enterprise/admin/pages/statisticsactordetail/statisticsactordetail.component';

const routes: Routes = [
  { path: '', component: PublichomeComponent },
  { path: 'categories', component: PubliccategoriesComponent },
  { path: 'category/:categoryCod', component: PubliccategoryComponent },
  { path: 'actor/:actorCod/fotos', component: PublicactorphotosComponent },
  { path: 'actor/:actorCod', component: PublicactorComponent },
  { path: 'video/:videoCod', component: PublicplayerComponent },
  { path: 'login', component: PublicloginComponent },
  { path: 'profile', component: PublicprofileComponent },
  { path: 'admin/login', component: LoginComponent },
  { path: 'admin', component: DashboardComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/videos', component: ListvideosComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/videos/create', component: CreatevideoComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/videos/edit/:videoCod', component: CreatevideoComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/categories', component: ListcatalogComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/actors', component: ListcatalogComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/tags', component: ListcatalogComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/capture-suggestions', component: CapturesuggestionsComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/statistics/videos', component: StatisticsvideosComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/statistics/videos/:videoCod', component: StatisticsvideodetailComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/statistics/actors', component: StatisticsactorsComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/statistics/actors/:actorCod', component: StatisticsactordetailComponent, canActivate: [AdminAuthGuard] },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
