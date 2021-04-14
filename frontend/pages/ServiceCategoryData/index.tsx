import { FC } from 'react'
import Layout from 'components/Layout'
// import { useRouter } from 'next/router'

export interface PortalAreaCategory {
    area: any
    name: string;
}

const ServiceCategoryData: FC<PortalAreaCategory> = ({area}) => {
    let test: boolean = true
    console.log(area)
    // const router = useRouter()
    // const { areaId } = router.query

    return (
        <Layout>
            {/* {console.log(area.name)} */}
            {test
                ? "Ikke implementert enda"
                : <div>real</div>
            }
        </Layout>
    )
}

export default ServiceCategoryData