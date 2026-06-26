import { Component, OnInit } from '@angular/core';
import { AdminVideoService } from '../../../video/service/AdminVideoService';

interface CaptureSuggestionGroup {
  VideoCod: string;
  Suggestions: any[];
}

@Component({
  selector: 'app-capturesuggestions',
  templateUrl: './capturesuggestions.component.html'
})
export class CapturesuggestionsComponent implements OnInit {
  suggestions: any[] = [];
  groups: CaptureSuggestionGroup[] = [];
  errorMessage = '';
  successMessage = '';
  batchLoading = false;

  constructor(private adminVideoService: AdminVideoService) {}

  async ngOnInit(): Promise<void> {
    await this.load();
  }

  async load(): Promise<void> {
    const rpt = await this.adminVideoService.findCaptureSuggestions();
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    this.suggestions = rpt.Data || [];
    this.groups = this.groupByVideo(this.suggestions);
  }

  async approve(item: any): Promise<void> {
    await this.review(item, true);
  }

  async reject(item: any): Promise<void> {
    await this.review(item, false);
  }

  async approveAll(): Promise<void> {
    if (!this.suggestions.length) return;
    const confirmed = window.confirm(`Se aprobaran ${this.suggestions.length} sugerencias pendientes. Desea continuar?`);
    if (!confirmed) return;
    this.errorMessage = '';
    this.successMessage = '';
    this.batchLoading = true;
    try {
      const rpt = await this.adminVideoService.approveAllCaptureSuggestions('');
      if (rpt.ErrorStatus) {
        this.errorMessage = rpt.Message;
        return;
      }
      this.successMessage = `Sugerencias aprobadas: ${rpt.Data?.ApprovedCount || 0}.`;
      await this.load();
    } finally {
      this.batchLoading = false;
    }
  }

  async approveGroup(group: CaptureSuggestionGroup): Promise<void> {
    if (!group.Suggestions.length) return;
    const confirmed = window.confirm(`Se aprobaran ${group.Suggestions.length} sugerencias del video ${group.VideoCod}. Desea continuar?`);
    if (!confirmed) return;
    this.errorMessage = '';
    this.successMessage = '';
    this.batchLoading = true;
    try {
      const rpt = await this.adminVideoService.approveVideoCaptureSuggestions(group.VideoCod, '');
      if (rpt.ErrorStatus) {
        this.errorMessage = rpt.Message;
        return;
      }
      this.successMessage = `Sugerencias aprobadas para ${group.VideoCod}: ${rpt.Data?.ApprovedCount || 0}.`;
      await this.load();
    } finally {
      this.batchLoading = false;
    }
  }

  private async review(item: any, approve: boolean): Promise<void> {
    this.errorMessage = '';
    this.successMessage = '';
    const rpt = approve
      ? await this.adminVideoService.approveCaptureSuggestion(item.SuggestionId, item.ReviewComment || '')
      : await this.adminVideoService.rejectCaptureSuggestion(item.SuggestionId, item.ReviewComment || '');
    if (rpt.ErrorStatus) {
      this.errorMessage = rpt.Message;
      return;
    }
    this.successMessage = approve ? 'Sugerencia aprobada.' : 'Sugerencia rechazada.';
    await this.load();
  }

  private groupByVideo(suggestions: any[]): CaptureSuggestionGroup[] {
    const groupMap = new Map<string, any[]>();
    for (const suggestion of suggestions) {
      const videoCod = suggestion.VideoCod || 'SIN_VIDEO';
      const items = groupMap.get(videoCod) || [];
      items.push(suggestion);
      groupMap.set(videoCod, items);
    }
    return Array.from(groupMap.entries()).map(([VideoCod, Suggestions]) => ({ VideoCod, Suggestions }));
  }
}
