import { defineStore } from "pinia";
import {
  CommunicationChannelPublicApiFrontendService,
  ContactApiConfig,
  ContactPublicApiFrontendService,
  IconHandlingApiConfig,
  IconHandlingPublicApiFrontendService,
  IconPublicApiFrontendService,
} from "@contacts/frontend-api";
import { Temporal } from "@js-temporal/polyfill";
import Instant = Temporal.Instant;
import { findAllContacts } from "@/utils/ContactUtils.ts";
import { env } from "@/env";

const contactApiConfig = new ContactApiConfig(env.contactHost, env.contactApi);
const iconHandlingApiConfig = new IconHandlingApiConfig(env.iconHandlingHost, env.iconHandlingApi);

export const myStore = defineStore("store", {
  
  state: () => ({
    contactApiService: new ContactPublicApiFrontendService(contactApiConfig),
    communicationChannelApiService: new CommunicationChannelPublicApiFrontendService(
      contactApiConfig,
    ),
    iconApiService: new IconPublicApiFrontendService(contactApiConfig),
    iconHandlingApiService: new IconHandlingPublicApiFrontendService(iconHandlingApiConfig),
    iconIdToPreviewUrl: new Map<bigint, string>(),
    iconIdToFullQualityUrl: new Map<bigint, string>(),
    contactIdToNextBirthdayInstant: new Map<bigint, Instant>(),
    contactsWithBirthdayNow: new Set<bigint>(),
    contactsWithBirthdaySoon: new Set<bigint>(),
    contacts: findAllContacts(),
    hasContactJustBeenReplaced: false,
  }),

  getters: {
    
  }
  
});
