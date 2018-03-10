import {Directive, Input, OnChanges, SimpleChanges} from "@angular/core";
import {
  AbstractControl,
  NG_VALIDATORS,
  NgModel,
  ValidationErrors,
  Validator,
  ValidatorFn,
  Validators
} from "@angular/forms";

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
