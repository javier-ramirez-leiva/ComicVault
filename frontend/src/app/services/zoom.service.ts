import { Injectable, Inject } from '@angular/core';
import { DOCUMENT } from '@angular/common';
import { consoleLog } from '../utils/traces';


//Works on PWA
@Injectable({ providedIn: 'root' })
export class ZoomService {
  private gestureHandler = this.preventZoom.bind(this);

  constructor(@Inject(DOCUMENT) private document: Document) { }

  private getViewportMeta(): HTMLMetaElement | null {
    return this.document.querySelector('meta[name=viewport]');
  }

  private createViewport(content: string): HTMLMetaElement {
    const m = this.document.createElement('meta');
    m.name = 'viewport';
    m.content = content;
    return m;
  }

  private forceRecalc() {
    // Force a reflow / layout calculation
    void this.document.body.offsetHeight;
    // small, non-destructive tweak to nudge re-render (restore after short delay)
    const html = this.document.documentElement;
    const orig = html.style.transform;
    html.style.transform = 'translateZ(0)'; // cheap GPU hint, triggers paint
    setTimeout(() => { html.style.transform = orig; }, 50);
  }

  private isStandalone(): boolean {
    return (window.matchMedia && window.matchMedia('(display-mode: standalone)').matches)
      || !!(navigator as any).standalone;
  }

  /* ---------- Public API ---------- */

  setZoomEnabled(value: boolean) {
    if (!value) {
      this.disableZoom();
    } else {
      this.enableZoom();
    }
  }

  private disableZoom() {
    // 1) try to set meta (may work on some Android)
    const content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no';
    const old = this.getViewportMeta();
    if (old) {
      // Replace meta node (replaceChild is sometimes more effective than setAttribute)
      const newMeta = this.createViewport(content);
      old.parentNode?.replaceChild(newMeta, old);
    } else {
      this.document.head.appendChild(this.createViewport(content));
    }

    // 2) Add iOS gesture blocker as defensive measure (no harm on Android)
    try {
      this.document.addEventListener('gesturestart', this.gestureHandler, { passive: false });
    } catch (e) {
      // ignore if browser doesn't support options
      this.document.addEventListener('gesturestart', this.gestureHandler);
    }

    // 3) minor nudge
    this.forceRecalc();
    consoleLog('[ZoomService] disableZoom: applied meta + gesture blocker');
  }

  private enableZoom(reloadIfNeeded = false) {
    // Try to re-enable zoom by replacing the meta and forcing a recalculation.
    const desired = 'width=device-width, initial-scale=1.0, maximum-scale=5.0, user-scalable=yes';
    const old = this.getViewportMeta();

    if (old) {
      // Replace meta node
      const newMeta = this.createViewport(desired);
      old.parentNode?.replaceChild(newMeta, old);
    } else {
      this.document.head.appendChild(this.createViewport(desired));
    }

    // Remove gesture blocker in case we previously added it
    try { this.document.removeEventListener('gesturestart', this.gestureHandler); } catch { }

    // Nudge layout
    this.forceRecalc();

    // After a short delay check whether the meta content actually changed
    setTimeout(() => {
      const current = this.getViewportMeta()?.content || '';
      const applied = current.indexOf('user-scalable=yes') > -1;
      consoleLog('[ZoomService] enableZoom: meta now ->', current, 'applied?', applied);

      // If it didn't apply, and user allowed reload, do a cache-busted replace to force a reload
      if (!applied && reloadIfNeeded) {
        consoleLog('[ZoomService] enableZoom: meta not applied; reloading to force change.');
        // Reload with cache-busting query param (preserves SPA route hash)
        const loc = this.document.location;
        const base = loc.pathname + loc.search;
        const hash = loc.hash || '';
        const sep = base.includes('?') ? '&' : '?';
        const newUrl = base + sep + '_rz=' + Date.now() + hash;
        // Use location.replace to avoid adding back/forward history
        loc.replace(newUrl);
      }
    }, 250);
  }

  // Debug helper to print the current viewport meta
  logViewport() {
    consoleLog('[ZoomService] viewport meta ->', this.getViewportMeta()?.content);
    consoleLog('[ZoomService] display-mode standalone ->', this.isStandalone());
    // visualViewport info (read-only)
    if ((window as any).visualViewport) {
      const vv = (window as any).visualViewport;
      consoleLog('[ZoomService] visualViewport scale:', vv.scale, 'width:', vv.width, 'height:', vv.height);
    }
  }

  private preventZoom(e: Event) {
    e.preventDefault();
  }
}