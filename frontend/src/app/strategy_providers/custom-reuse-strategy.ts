import { RouteReuseStrategy, DetachedRouteHandle, ActivatedRouteSnapshot } from '@angular/router';

const storedRoutes = ['search', /*library*/'series', /*library*/'issues', 'home'] as const;
let storedRouteString = ''
export const nonResetRoutes = ['comics/:id/details', 'comics-search/:idGc/details', 'series/:id/details'] as const;

export function isStoreRoute(route: string): boolean {
    return storedRoutes.some(r => route === r);
}

export function isNonResetRoute(route: string): boolean {
    return nonResetRoutes.some(r => route.startsWith(r));
}

export function isStoredRoute(route: string): boolean {
    return storedRoutes.some(r => route.startsWith(r));
}

export function resetRouteCache() {
    storedRouteString = '';
}

//The strategy only stores one route at a time. Any new route resets the cache, unless the nonResetRoutes that are the routes from where we expect to go back
export class CustomReuseStrategy implements RouteReuseStrategy {
    private storedRoute: DetachedRouteHandle | null = null;
    private scrollPosition: number = 0;

    //Resets the cache and stores scroll also 
    shouldDetach(route: ActivatedRouteSnapshot): boolean {
        if (!isNonResetRoute(route.routeConfig?.path ?? '')) {
            resetRouteCache();
        }
        const isStoreRouteResult = isStoreRoute(route.routeConfig?.path ?? '');
        if (isStoreRouteResult) {
            this.scrollPosition = window.scrollY;
        }
        return isStoreRouteResult ? true : false;
    }

    store(route: ActivatedRouteSnapshot, handle: DetachedRouteHandle): void {
        storedRouteString = route.routeConfig?.path ?? '';
        this.storedRoute = handle;
    }

    //This method is always called. This method also clears the cache on any route change, unless it is a nonResetRoute
    shouldAttach(route: ActivatedRouteSnapshot): boolean {
        return route.routeConfig?.path === storedRouteString;
    }

    retrieve(route: ActivatedRouteSnapshot): DetachedRouteHandle | null {
        setTimeout(() => {
            window.scrollTo({ top: this.scrollPosition, behavior: 'instant' });
        }, 0);
        return this.storedRoute;
    }

    shouldReuseRoute(future: ActivatedRouteSnapshot, curr: ActivatedRouteSnapshot): boolean {
        return future.routeConfig === curr.routeConfig;
    }
}