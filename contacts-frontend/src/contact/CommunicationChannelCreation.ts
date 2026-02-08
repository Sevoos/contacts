import {CommunicationChannelCreationDto, EnumCommunicationChannel} from "@/lib";

export class CommunicationChannelCreation {

  constructor(
    public comment: string | null,
    public type: string,
    public contactId: bigint,
    public value: string
  ) {
  }

  toDto(): CommunicationChannelCreationDto {
    return new CommunicationChannelCreationDto(
      this.comment,
      EnumCommunicationChannel.valueOf(this.type),
      this.contactId,
      this.value
    )
  }

  equals(other: CommunicationChannelCreation): boolean {
    return JSON.stringify(this) === JSON.stringify(other)
  }

}
