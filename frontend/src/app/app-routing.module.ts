import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './enterprise/admin/pages/dashboard/dashboard.component';
import { LoginComponent } from './enterprise/admin/pages/login/login.component';
import { ListcatalogComponent } from './enterprise/admin/pages/listcatalog/listcatalog.component';
import { ListvideosComponent } from './enterprise/admin/pages/listvideos/listvideos.component';
import { CreatevideoComponent } from './enterprise/admin/pages/createvideo/createvideo.component';
import { AdminAuthGuard } from './enterprise/shared/guard/AdminAuthGuard';
import { PubliccategoryComponent } from './enterprise/video/pages/publiccategory/publiccategory.component';
import { PublichomeComponent } from './enterprise/video/pages/publichome/publichome.component';
import { PublicplayerComponent } from './enterprise/video/pages/publicplayer/publicplayer.component';

const routes: Routes = [
  { path: '', component: PublichomeComponent },
  { path: 'category/:categoryCod', component: PubliccategoryComponent },
  { path: 'video/:videoCod', component: PublicplayerComponent },
  { path: 'admin/login', component: LoginComponent },
  { path: 'admin', component: DashboardComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/videos', component: ListvideosComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/videos/create', component: CreatevideoComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/videos/edit/:videoCod', component: CreatevideoComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/categories', component: ListcatalogComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/actors', component: ListcatalogComponent, canActivate: [AdminAuthGuard] },
  { path: 'admin/tags', component: ListcatalogComponent, canActivate: [AdminAuthGuard] },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
