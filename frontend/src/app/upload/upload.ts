import { Component, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common'; 
import { HttpClient } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { DEFAULT_DATE_FORMAT } from '../constants';

@Component({
  selector: 'app-upload',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './upload.html',
  styleUrls: ['./upload.scss']
})
export class UploadComponent {
  @ViewChild('fileInput') inputRef!: ElementRef<HTMLInputElement>;
  selectedFile: File | null = null;
  message = '';
  errorMessage = '';
  detailedMessage = '';

  resultRows: { emp1: string; emp2: string; projectId: string; days: number }[] = [];
  totalTime = '0';

  DEFAULT_DATE_FORMAT = DEFAULT_DATE_FORMAT;
  dateFormat = DEFAULT_DATE_FORMAT;

  constructor(private http: HttpClient) {}


  onFileSelected(event: any): void {
    this.clear();
    this.selectedFile = event.target.files[0];
  }

  onUpload() {
    if (!this.selectedFile) {
      this.message = 'Select a file!';
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('dateFormat', this.dateFormat || DEFAULT_DATE_FORMAT);
    
    const body = {
      dateFormat: this.dateFormat,
    };

    this.http.post<any>('http://localhost:8080/api/employee-pairs/analyze', formData)
      .subscribe({
        next: (response) => {
          this.errorMessage = '';
          this.message = response.maxPairResultMessage;
          const emp1 = response.employee1;
          const emp2 = response.employee2;
          const allProjects = response.allCommonProjects;

          this.resultRows = Object.entries(allProjects).map(([projectId, days]) => ({
            emp1,
            emp2,
            projectId,
            days: Number(days)
          }));

          this.totalTime = response.time;
        },
        error: (err) => {
          console.error(err);
          this.message = '';
          this.errorMessage =  "Upload and processing of the file failed. ";
          this.detailedMessage = "Detailed error: " + err.error;
        }
      });
  }

  clear() {
    this.message = '';
    this.resultRows = [];
    this.errorMessage = '';
    this.detailedMessage = '';
  }

  clearAll() {
    this.clear();
    this.selectedFile = null;
    this.inputRef.nativeElement.value = '';
  }
}
