export class Icon {
  constructor(
    public id: bigint,
    public url: string
  ) {
  }

  public copy(): Icon {
    return new Icon(
      this.id,
      this.url
    )
  }

  public equals(other: Icon): boolean {
    return this.id === other.id
  }

}
