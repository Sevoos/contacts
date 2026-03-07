import {CommunicationChannelContactCreationDto, EnumCommunicationChannel} from "@/lib";

export class CommunicationChannelContactCreation {

  constructor(
    public comment: string | null,
    public type: string,
    public value: string
  ) {
  }

}

export function comChanContactCreationToDto(object: CommunicationChannelContactCreation): CommunicationChannelContactCreationDto {
  return new CommunicationChannelContactCreationDto(
    object.comment,
    EnumCommunicationChannel.valueOf(object.type),
    object.value
  )
}

export function doComChannelsContactCreationsEqual(
  a: CommunicationChannelContactCreation,
  b: CommunicationChannelContactCreation
): boolean {
  return JSON.stringify(a) === JSON.stringify(b)
}

export function comChanContactCreationFromDto(dto: CommunicationChannelContactCreationDto): CommunicationChannelContactCreation {
  return new CommunicationChannelContactCreation(
    dto.comment ?? null,
    dto.type.name,
    dto.value
  )
}
