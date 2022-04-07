/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

import {Injectable} from '@angular/core';
import {HttpErrorResponse} from '@angular/common/http';

import {Observable, of} from 'rxjs';

import {MessageService} from './message.service';

/** Type of the handleError function returned by HttpErrorHandler.createHandleError */
export type HandleError =
  <T> (operation?: string, result?: T) => (error: HttpErrorResponse) => Observable<T>;

/** Handles HttpClient errors */
@Injectable()
export class HttpErrorHandler {
  constructor(private messageService: MessageService) {
  }

  /** Create curried handleError function that already knows the service name */
  createHandleError = (serviceName = '') =>
    <T>(operation = 'operation', result = {} as T) =>
      this.handleError(serviceName, operation, result);

  /**
   * Returns a function that handles Http operation failures.
   * This error handler lets the app continue to run as if no error occurred.
   *
   * @param serviceName = name of the data service that attempted the operation
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  handleError<T>(serviceName = '', operation = 'operation', result = {} as T) {

    return (error: HttpErrorResponse): Observable<T> => {
      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      const message = (error.error instanceof ErrorEvent) ?
        error.error.message :
        `server returned code ${error.status} with body "${error.error}"`;

      // TODO: better job of transforming error for user consumption
      this.messageService.add(`${serviceName}: ${operation} failed: ${message}`);

      // Let the app keep running by returning a safe result.
      return of(result);
    };

  }
}


/*
Copyright Google LLC. All Rights Reserved.
Use of this source code is governed by an MIT-style license that
can be found in the LICENSE file at https://angular.io/license
*/
