import {
  type CommunicationChannelDto,
  EnumCommunicationChannel
} from "@contacts/frontend-api";

export class CommunicationChannel {

  constructor(
    public id: bigint,
    public comment: string | null,
    public type: string,
    public value: string
  ) {
  }

}

export function communicationChannelFromDto(dto: CommunicationChannelDto): CommunicationChannel {
  return new CommunicationChannel(
    dto.id,
    dto.comment ?? null,
    dto.type.name,
    dto.value
  )
}

export const communicationChannelTypes = EnumCommunicationChannel.values().map(value => value.name)

export function performErrorCheck(type: string, value: string): string | null {
  if (type === EnumCommunicationChannel.Email.name) {
    function isValidEmail(email: string): boolean {
      const pattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
      return pattern.test(email);
    }
    if (!isValidEmail(value)) {
      return "Invalid email address"
    }
  }
  return null
}
