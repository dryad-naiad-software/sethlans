/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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
 *
 */

import {Directive, Input, OnChanges, SimpleChanges} from '@angular/core';
import {AbstractControl, NG_VALIDATORS, NgModel, ValidationErrors, Validator, ValidatorFn, Validators} from '@angular/forms';

@Directive({
  selector: '[fieldMatches]',
  providers: [{
    provide: NG_VALIDATORS,
    useExisting: FieldMatchesValidatorDirective,
    multi: true
  }]
})
export class FieldMatchesValidatorDirective implements Validator, OnChanges {
  @Input() fieldMatches: NgModel;

  private validationFunction = Validators.nullValidator;

  ngOnChanges(changes: SimpleChanges): void {
    let change = changes['fieldMatches'];
    if (change) {
      const otherFieldModel = change.currentValue;
      this.validationFunction = fieldMatchesValidator(otherFieldModel);
    } else {
      this.validationFunction = Validators.nullValidator;
    }
  }

  validate(control: AbstractControl): ValidationErrors | any {
    return this.validationFunction(control);
  }
}

export function fieldMatchesValidator(otherFieldModel: NgModel): ValidatorFn {
  return (control: AbstractControl): ValidationErrors => {
    return control.value === otherFieldModel.value ? null : {'fieldMatches': {match: false}};
  };
}
