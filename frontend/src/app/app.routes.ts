import { Routes } from '@angular/router';
import { HomePageComponent, JobPageComponent } from 'components';
import { SearchPageComponent } from 'components';
import { ComicSearchDetailsComponent } from 'components';
import { DownloadsPageComponent } from 'components';
import { ComicDatabaseDetailsComponent } from 'components';
import { ReaderPageComponent } from 'components';
import { SettingsPageComponent } from 'components';
import { SeriesDetailsComponent } from 'components';
import { LogHistoryPageComponent } from 'components';
import { LibraryPageComponent } from 'components';
import { LoginPageComponent } from 'components';
import { authGuard } from './guard/auth.guard';
import { registerGuard } from './guard/register.guard';
import { RegisterPageComponent } from 'components';
import { UsersPageComponent } from 'components';
import { loginGuard } from './guard/login.guard';
import { UserPageComponent } from 'components';
import { DevicesPageComponent } from 'components';
import { UnsavedChangesGuard } from './guard/unsaved-changes-guard.guard';
import { libraryRedirectGuard } from './guard/library-redicrect-guard';
import { JobsPageComponent } from './components/jobs-page/jobs-page.component';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/home',
    pathMatch: 'full'
  },
  {
    path: 'login',
    component: LoginPageComponent,
    title: 'Login',
    canActivate: [loginGuard]
  },
  {
    path: 'home',
    component: HomePageComponent,
    title: 'Home',
    canActivate: [authGuard]
  },
  {
    path: 'library',
    canActivate: [authGuard],
    children: [
      {
        path: '', // when hitting /library
        pathMatch: 'full',
        canActivate: [libraryRedirectGuard], // guard decides series/issues
        component: LibraryPageComponent // 👈 REQUIRED parent outlet host
      },
      {
        path: 'series',
        component: LibraryPageComponent,
        title: 'Library'
      },
      {
        path: 'issues',
        component: LibraryPageComponent,
        title: 'Library'
      }
    ]
  },
  {
    path: 'search',
    component: SearchPageComponent,
    title: 'Search',
    canActivate: [authGuard]
  },
  {
    path: 'downloads',
    component: DownloadsPageComponent,
    title: 'Downloads',
    canActivate: [authGuard]
  },
  {
    path: 'settings',
    component: SettingsPageComponent,
    title: 'Settings',
    canActivate: [authGuard]
  },
  {
    path: 'users',
    component: UsersPageComponent,
    title: 'Users',
    canActivate: [authGuard]
  },
  {
    path: 'devices',
    component: DevicesPageComponent,
    title: 'Devices',
    canActivate: [authGuard]
  },
  {
    path: 'settings/logs',
    component: LogHistoryPageComponent,
    title: 'Logs',
    canActivate: [authGuard]
  },
  {
    path: 'settings/jobs',
    component: JobsPageComponent,
    title: 'Jobs',
    canActivate: [authGuard]
  },
  {
    path: 'settings/job/:id/details',
    component: JobPageComponent,
    title: 'Jobs Details',
    canActivate: [authGuard]
  },
  {
    path: 'comics/:id/details',
    component: ComicDatabaseDetailsComponent,
    title: 'Comic Details',
    canActivate: [authGuard],
    canDeactivate: [UnsavedChangesGuard]
  },
  {
    path: 'comics-search/:idGc/details',
    component: ComicSearchDetailsComponent,
    title: 'Comic Search Details',
    canActivate: [authGuard]
  },
  {
    path: 'comics/:id/read',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        redirectTo: 'standard',
        pathMatch: 'full'
      },
      {
        path: 'standard',
        component: ReaderPageComponent,
        title: 'Reader'
      },
      {
        path: 'incognito',
        component: ReaderPageComponent,
        title: 'Reader incognito'
      }
    ]
  },
  {
    path: 'series/:id/details',
    component: SeriesDetailsComponent,
    title: 'Details',
    canActivate: [authGuard],
    canDeactivate: [UnsavedChangesGuard]
  },
  {
    path: 'user',
    component: UserPageComponent,
    title: 'User',
    canActivate: [authGuard]
  },
  {
    path: 'user/:username',
    component: UserPageComponent,
    title: 'User',
    canActivate: [authGuard]
  },
  {
    path: 'register',
    component: RegisterPageComponent,
    title: 'Register',
    canActivate: [registerGuard]
  }
];