import {
  type CommunicationChannelDto,
  EnumCommunicationChannel
} from "@/lib";

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
