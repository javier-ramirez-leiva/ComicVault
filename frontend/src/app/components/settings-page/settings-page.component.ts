import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { JDownloaderConfiguration, ServerInfo, SlackConfiguration } from 'interfaces';
import { TestButtonComponent } from '../test-button/test-button.component';
import { FormsModule } from '@angular/forms';
import { Configuration } from 'interfaces';
import { RouterModule } from '@angular/router';
import { DarkmodeService, NotifierService, ModalService, ConfigService, PaginationRatioService, ServerInfoService } from 'services';
import { UntilDestroy, untilDestroyed } from '@ngneat/until-destroy';
import { HideRolesDirective } from 'directives';
import { Role } from 'interfaces';
import { ExpanderButtonComponent } from "../expander-button/expander-button.component";
import { ScanLibButtonComponent } from "../scan-lib-button/scan-lib-button.component";
import { CleanLibButtonComponent } from "../clean-lib-button/clean-lib-button.component";
import { DeleteLibButtonComponent } from '../delete-lib-button/delete-lib-button.component';
import { Subject, switchMap } from 'rxjs';
import { BooleanSliderComponent } from "../boolean-slider/boolean-slider.component";
import { DigitInputComponent } from "../digit-input/digit-input.component";
import { InputTextComponent } from "../input-text/input-text.component";
import { HelpButtonComponent } from "../help-button/help-button.component";
import { ModalServerInfoComponent } from '../modal-server-info/modal-server-info.component';


@UntilDestroy()
@Component({
    selector: 'app-settings-page',
    imports: [CommonModule, TestButtonComponent, FormsModule, RouterModule, HideRolesDirective, ExpanderButtonComponent, ScanLibButtonComponent, CleanLibButtonComponent, DeleteLibButtonComponent, BooleanSliderComponent, DigitInputComponent, InputTextComponent, HelpButtonComponent],
    templateUrl: './settings-page.component.html',
    styleUrl: './settings-page.component.css'
})
export class SettingsPageComponent {
    showServer: boolean = false;
    showTheme: boolean = false;
    showSlack: boolean = false;
    showComicProviders: boolean = false;
    showComicVine: boolean = false;
    showAdvanced: boolean = false;
    showJDownloader: boolean = false;
    config!: Configuration;
    storagePath: string = '';
    getcomicsBaseUrl: string = '';
    comicVineAPIKey: string = '';
    slackConfiguration: SlackConfiguration = {
        slackWebHook: '',
        enableNotifications: false,
        comicVaultBaseUrl: '',
    }
    jdownloaderConfiguration: JDownloaderConfiguration = {
        jdownloaderCrawljobPath: '',
        jdownloaderOutputPath: '',
        deleteFolderOutputFolder: false
    };
    scanArchives: boolean = false;
    deleteArchives: boolean = false;
    generateNavigationThumbnails: boolean = false;
    darkMode: boolean = false;
    paginationRatio: number = 1;
    displayPaginationTooltip: boolean = false;

    resetStorageStatus$ = new Subject<void>();
    resetSlackStatus$ = new Subject<void>();
    resetJdownloaderStatus$ = new Subject<void>();

    protected readonly notifierService: NotifierService = inject(NotifierService);
    protected readonly modalService = inject(ModalService);
    protected readonly configService = inject(ConfigService);
    protected readonly darkModeService = inject(DarkmodeService);
    protected readonly paginationRatioService = inject(PaginationRatioService);
    private readonly serverInfoService = inject(ServerInfoService);


    Role = Role;

    private getconfig() {
        this.darkModeService.isDarkMode$.pipe(untilDestroyed(this)).subscribe((isDarkMode) => { this.darkMode = isDarkMode; });
        this.paginationRatio = this.paginationRatioService.getPaginationRatio();
        this.configService.getConfig().pipe(untilDestroyed(this)).subscribe((comicConfig) => {
            this.storagePath = comicConfig.downloadRoot;
            this.slackConfiguration = comicConfig.slackConfiguration;
            this.jdownloaderConfiguration = comicConfig.jdownloaderConfiguration;
            this.getcomicsBaseUrl = comicConfig.getComicsBaseUrl;
            this.comicVineAPIKey = comicConfig.comicVine_apiKey;
            this.scanArchives = comicConfig.scanArchives;
            this.deleteArchives = comicConfig.deleteArchives;
            this.generateNavigationThumbnails = comicConfig.generateNavigationThumbnails;
        });
    }

    ngOnInit() {
        this.getconfig();
    }

    sendEnableNotifications() {
        this.configService.setEnableSlackNotifications(this.slackConfiguration.enableNotifications).pipe(untilDestroyed(this)).subscribe();
    }

    sendScanArchives() {
        this.configService.setScanArchives(this.scanArchives).pipe(untilDestroyed(this)).subscribe();
    }

    sendDeleteArchives() {
        this.configService.setDeleteArchives(this.deleteArchives).pipe(untilDestroyed(this)).subscribe();
    }

    sendGenerateNavigationThumbnails() {
        this.configService.setGenerateNavigationThumbnails(this.generateNavigationThumbnails).pipe(untilDestroyed(this)).subscribe();
    }

    toggleShowServer() {
        this.showServer = !this.showServer;
    }

    toggleShowTheme() {
        this.showTheme = !this.showTheme;
    }

    toggleShowSlack() {
        this.showSlack = !this.showSlack;
    }

    toggleComicProviders() {
        this.showComicProviders = !this.showComicProviders;
    }

    toggleShowComicVine() {
        this.showComicVine = !this.showComicVine;
    }

    toggleAdvanced() {
        this.showAdvanced = !this.showAdvanced;
    }
    toggleJDownloader() {
        this.showJDownloader = !this.showJDownloader;
    }

    toggleDarkMode() {
        this.darkModeService.updateDarkMode();
    }


    updatePaginationRatio() {
        this.paginationRatioService.setPaginationRatio(this.paginationRatio);
    }

    displayServerInfo() {
        this.serverInfoService.getServerInfo().pipe(
            switchMap(serverInfo => this.modalService.open<null, { serverInfo: ServerInfo | undefined }>(ModalServerInfoComponent, { serverInfo: serverInfo })),
            untilDestroyed(this)
        ).subscribe();
    }

}
