import Alertstripe from 'nav-frontend-alertstriper'

import { fetchData } from './fetchServices'

export const mapStatusAndIncidentsToArray = (areas) => {
    let areasArray: Array<String> = []
    areas.map(area => {
        areasArray.push(area)
    })
    return areasArray;
}

export const retrieveFilteredServiceList = (areas, areaName) => {
    const filteredArea = areas.find(
        area => area.name == areaName
    )
    return filteredArea
}

export const countServicesInAreas = (mappedAreas) => {
    let numberOfServices: number = 0;
    mappedAreas.forEach(function (area){
        numberOfServices += area.services.length
    })
    return numberOfServices
}

export const countHealthyServices = (mappedAreas) => {
    let healthyServices: number = 0;
    mappedAreas.map(area => {
        healthyServices += area.services.filter(
            (service: any) => service.status !== "DOWN").length
    })
    return healthyServices
}

