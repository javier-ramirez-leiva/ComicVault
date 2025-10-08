import { CommonModule } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Severity } from 'interfaces';
import { EmptyToNAPipe } from "../../pipes/empty-to-na.pipe";
import { FormGroup, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { InputTextFormComponent } from "../input-text-form/input-text-form.component";
import { DigitInputFormComponent } from "../digit-input-form/digit-input-form.component";

@Component({
  selector: 'app-two-columns-table',
  imports: [CommonModule, RouterModule, EmptyToNAPipe, FormsModule, ReactiveFormsModule, InputTextFormComponent, DigitInputFormComponent],
  templateUrl: './two-columns-table.component.html'
})
export class TwoColumnsTableComponent {
  @Input({ required: true }) rows: Row[] = [];
  @Input({ required: false }) modalMode: boolean = false;
  @Input({ required: false }) editMode: boolean = false;
  @Input({ required: false }) formTable: FormGroup | undefined = undefined;

}

export type Row =
  | {
    title: string;
    type: 'image';
    src: string;
    alt: string;
  }
  | {
    title: string;
    type: 'text';
    text: string;
  }
  | {
    title: string;
    type: 'link';
    link: string;
  } |
  {
    title: string;
    type: 'routerLink';
    text: string;
    routerLink: string[];
  } |
  {
    title: string;
    type: 'chip';
    text: string;
    severity: Severity;
  } |
  {
    title: string;
    type: 'editable-text';
    formControlName: string;
    text: string;
  } |
  {
    title: string;
    type: 'editable-number';
    formControlName: string;
    number: number;
  } |
  {
    title: string;
    type: 'editable-link';
    formControlName: string;
    link: string;
  }