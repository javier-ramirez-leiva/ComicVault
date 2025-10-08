import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DeviceInfo } from 'interfaces';
import { HttpService } from './http.service';
import { NotifierService } from './notifier.service';

@Injectable({
    providedIn: 'root'
})
export class DevicesService {

    private readonly httpService = inject(HttpService);
    private readonly notifier = inject(NotifierService);

    allDevices(): Observable<DeviceInfo[]> {
        return this.httpService.request<DeviceInfo[]>(
            'GET',
            `/devices`,
        );
    }

    deleteDevice(id: string): Observable<any> {
        return this.httpService.request<any>(
            'DELETE',
            `/devices/${id}`,
        );
    }
}
