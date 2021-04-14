import { fetchData } from './fetchServices'

export const mapStatusAndIncidentsToArray = (areas) => {
    let areasArray: Array<String> = []
    areas.map(area => {
        areasArray.push(area)
    })
    return areasArray;
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


export const checkStatus = () =>   {
    const areas: any = fetchData
    areas.map((area) => {

    })
}

