import { useEffect, useState } from "react";


async function fetchData() {
    console.log("fetch")
    const response = await fetch("http://localhost:3001/rest/v0.1/testAreas");
    const data = await response.json()
    return data
}

function FetchNavDigitalServices() {
    const [services, setServices] = useState([])
    const [isLoading, setIsLoading] = useState(false)

    useEffect(() => {
        (async function () {
            setIsLoading(true)
            const newServices = await fetchData()
            setServices(newServices)
            setIsLoading(false)
        })()
    }, [])

    if (isLoading) {
        return <p>Loading services...</p>
    }

    return (
        <ul>{services.map((service) => <li key={service.name}>{service.name}: {service.status}</li>)}</ul>
    )
}

export default FetchNavDigitalServices