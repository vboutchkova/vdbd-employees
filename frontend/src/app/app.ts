import { Component, signal } from '@angular/core';
import { UploadComponent } from './upload/upload';
import { provideHttpClient } from '@angular/common/http'

@Component({
  selector: 'app-root',
  imports: [ UploadComponent ],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly title = signal('frontend');
}
