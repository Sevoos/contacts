import {toRaw} from "vue";

export class InputArray<T> {

  public array: InputItem<T>[]
  public currentError: string | null
  public currentInput: T

  constructor(
    private emptyValueSupplier: () => T,
    initialArray: T[] = [],
    private predicateIsValueNotEmpty: (value: T) => boolean,
    private performErrorCheck: (value: T) => string | null,
    private pushedValueTransformer: (value: T) => T,
    private doValuesEqual: (a: T, b: T) => boolean = (a, b) => a === b
  ) {
    this.array = initialArray.map(value => newInputItem(value))
    this.currentError = null
    this.currentInput = emptyValueSupplier()
  }

  private isThereAnError(): boolean {
    return this.currentError !== null
  }

  private transformedInput(): T {
    return this.pushedValueTransformer(this.currentInput)
  }

  public addCurrentInputToArray() {
    const transformedInput = this.transformedInput()
    this.currentError = this.performErrorCheck(transformedInput)
    if (this.isThereAnError()) return
    this.array.push(newInputItem(transformedInput))
    this.currentInput = this.emptyValueSupplier()
  }

  public canAddNewItem(): boolean {
    return !this.isThereAnError() && this.predicateIsValueNotEmpty(this.transformedInput())
  }

  public deleteItem(index: number) {
    this.array.splice(index, 1)
  }

  public discardEditedItem(index: number) {
    const item = this.array[index]!
    item.editedValue = structuredClone(toRaw(item.savedValue))
    item.isEditing = false
    item.error = null
  }

  public getItem(index: number): InputItem<T> {
    return this.array[index]!
  }

  public saveItem(index: number) {
    const item = this.getItem(index)
    const editedValue = this.pushedValueTransformer(item.editedValue)
    if (!this.predicateIsValueNotEmpty(editedValue)) {
      item.error = "Item can't be empty"
      return
    }
    item.error = this.performErrorCheck(editedValue)
    if (item.error) return
    item.editedValue = structuredClone(editedValue)
    item.savedValue = structuredClone(editedValue)
    item.isEditing = false
  }

  public startEditing(index: number) {
    this.array[index]!.isEditing = true
  }

  public getOnlySavedValues(): T[] {
    return this.array.map(value => value.savedValue)
  }

  public getOnlyEditedValue(): T[] {
    return this.array.map(value => value.editedValue)
  }

  public copy(): InputArray<T> {
    return new InputArray<T>(
      this.emptyValueSupplier,
      this.array.map(value => value.savedValue),
      this.predicateIsValueNotEmpty,
      this.performErrorCheck,
      this.pushedValueTransformer,
      this.doValuesEqual
    )
  }

  private doTransformedValuesEqual(a: T, b: T): boolean {
    return this.doValuesEqual(this.pushedValueTransformer(a), this.pushedValueTransformer(b))
  }

  public hasItemChanged(index: number): boolean {
    const item = this.array[index]!
    return !this.doTransformedValuesEqual(item.savedValue, item.editedValue)
  }

}

export type InputItem<T> = {
  editedValue: T
  error: string | null
  isEditing: boolean
  savedValue: T
}

function newInputItem<T>(value: T): InputItem<T> {
  return {
    editedValue: structuredClone(toRaw(value)),
    error: null,
    isEditing: false,
    savedValue: structuredClone(toRaw(value))
  }
}
