export class ConfigItemPatch<T> {

  public currentPatch: T
  public editedPatch: T
  public error: string | null = null
  public isEditing = false

  constructor(
    private allowedToBeEmpty: (editedValue: T) => boolean,
    public initialValue: T,
    private valueTransformer: ((value: T) => T) | null = null,
    private isValueEmpty: (value: T) => boolean = (value: T) => !(value),
    private copyValue: (value: T) => T = (source: T) => source,
    private errorCheck: (value: T) => (string | null) = () => null,
    private doValuesEqual: (a: T, b: T) => boolean = (a: T, b: T) => a === b
  ) {
    this.currentPatch = copyValue(initialValue)
    this.editedPatch = copyValue(initialValue)
  }

  private performErrorCheck(value: T) {
    if (this.isValueEmpty(value) && !this.allowedToBeEmpty(value)) {
      this.error = "Value can't be empty"
      return
    }
    this.error = this.errorCheck(value)
  }

  private transformValue(value: T): T {
    if (this.valueTransformer === null) {
      return value
    }
    return this.valueTransformer(value)
  }

  public discard() {
    this.removeError()
    this.isEditing = false
    this.editedPatch = this.copyValue(this.currentPatch)
  }

  public getCurrentPatchUnlessEmpty(): T | null {
    return this.isValueEmpty(this.currentPatch) ? null : this.currentPatch
  }

  public getEditedPatchUnlessEmpty(): T | null {
    return this.isValueEmpty(this.editedPatch) ? null : this.editedPatch
  }

  public hasEditedPatchChanged(): boolean {
    return !this.doValuesEqual(this.currentPatch, this.transformValue(this.editedPatch))
  }

  public isEditedEmpty(): boolean {
    return this.isValueEmpty(this.editedPatch)
  }

  public removeError() {
    this.error = null
  }

  public revert() {
    this.isEditing = false
    this.currentPatch = this.copyValue(this.initialValue)
    this.editedPatch = this.copyValue(this.initialValue)
  }

  public save() {
    const transformedValue = this.transformValue(this.editedPatch)
    this.editedPatch = this.copyValue(transformedValue)
    this.performErrorCheck(transformedValue)
    if (this.error) return
    this.currentPatch = this.copyValue(transformedValue)
    this.isEditing = false
  }

  public startEditing() {
    this.editedPatch = this.copyValue(this.currentPatch)
    this.isEditing = true
  }

  public isDifferentFromInitialValue(): boolean {
    if (this.isValueEmpty(this.initialValue) && this.isValueEmpty(this.currentPatch)) {
      return false
    }
    return !this.doValuesEqual(this.initialValue, this.currentPatch)
  }

}
