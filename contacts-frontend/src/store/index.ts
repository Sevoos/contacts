import { defineStore } from "pinia"
import {
  ContactApiConfig, ContactPublicApiFrontendService, IconHandlingApiConfig,
  IconHandlingPublicApiFrontendService, IconPublicApiFrontendService
} from "@/lib";
import {Temporal} from "@js-temporal/polyfill";
import Instant = Temporal.Instant;

const contactApiConfig= new ContactApiConfig("http://localhost:8000", "/api/contact/v1")
const iconHandlingApiConfig = new IconHandlingApiConfig("http://localhost:8000", "/api/icon-handling/v1")

export const myStore = defineStore("store", {

  state: () => ({
    contactApiService: new ContactPublicApiFrontendService(contactApiConfig),
    iconApiService: new IconPublicApiFrontendService(contactApiConfig),
    iconHandlingApiService: new IconHandlingPublicApiFrontendService(iconHandlingApiConfig),
    // count: 0,
    iconIdToPreviewUrl: new Map<bigint, string>,
    iconIdToFullQualityUrl: new Map<bigint, string>,
    contactIdToNextBirthdayInstant: new Map<bigint, Instant>,
    contactsWithBirthdayNow: new Set<bigint>,
    contactsWithBirthdaySoon: new Set<bigint>,
  }),

  getters: {
    // double: (state) => state.count * 2
  }

})
