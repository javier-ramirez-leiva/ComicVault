export enum Role {
    Admin = 'ADMIN',
    Owner = 'OWNER',
    Contributor = 'CONTRIBUTOR',
    Requester = 'REQUESTER',
    Viewer = 'VIEWER'
}

export interface AuthentificationRequest {
    username: string;
    password: string;
    userAgent: string,
    os: string,
    browser: string,
    device: string,
    osVersion: string,
    browserVersion: string,
    orientation: string
}

export interface DeviceInfo {
    id: string;
    username: string;
    userAgent: string,
    os: string,
    browser: string,
    device: string,
    osVersion: string,
    browserVersion: string,
    orientation: string,
    createdAt: Date;
    ip: string
}

export interface AuthentificationResponse {
    role: Role;
    username: string;
    color: string;
}

export interface RegisterRequest {
    username: string;
    password: string;
    role: Role;
    color: string;
}

export interface UserInfoResponse {
    username: string;
    role: Role;
    color: string;
}

