import React, {useEffect, useRef, useState} from "react"
import MainLayout from "../../layouts/MainLayout";
import {Title} from "./Map.styles"
import ReactMapGL, {GeolocateControl, Layer, Marker, NavigationControl, Popup, Source, useControl} from 'react-map-gl';
import 'mapbox-gl/dist/mapbox-gl.css'
import MapService from "src/service/Map/MapService";
import {coordinate, initialCreateBooking} from "src/@types/map";
import 'antd/dist/antd.css';
import {MessageLoadMapService} from "src/service/Message/MessageService";
import {CarOutlined, EnvironmentOutlined} from '@ant-design/icons'
import {COLOR} from "src/constants/styles";
import {useSpring,animated} from 'react-spring'
import MapBoxGeocoder from '@mapbox/mapbox-gl-geocoder';
import {connect, ConnectedProps, useSelector} from "react-redux"
import {location, info2Location, featuresLocation} from "src/@types/bookingcar";
import { Drawer } from "antd";
import bookingCar from "../BookingCar/BookingCar";
import {BODYSTATES} from "../../constants/states";
import {objectTraps} from "immer/dist/core/proxy";
const accessToken = "pk.eyJ1IjoicGhhbXRpZW5xdWFuIiwiYSI6ImNsNXFvb2h3ejB3NGMza28zYWx2enoyem4ifQ.v-O4lWtgCXbhJbPt5nPFIQ";



const Menu = (props:any) => {
  const {location,drawer}=props;
  const [toggle, setToggle] = useState(false);
  const fade=useSpring({
    opacity:toggle?0:1
  })
  return (
    <div
      style={{position: "absolute",left:'5px',top:'5px',maxWidth: "320px"}}
    >
      <button className="btn  btn-info btn-sm" style={{position: "relative",width:'120px'}} onClick={() => setToggle(!toggle)}>{toggle?'Hiện thông tin':'Ẩn thông tin'}</button>
      <button className="btn  btn-info btn-sm ml-2" onClick={drawer}>Hiện trạng thái đặt xe</button>
      <animated.form style={fade} className="p-5 rounded-sm shadow text-center info-background" hidden={toggle}>
        <Title>Thông tin đặt xe</Title>
        <label className="float-left mb-1">Địa điểm đón <CarOutlined/> </label>
        <input
          type="text"
          className="form-control form-control-lg mb-3"
          readOnly={true}
          value={location.departure}
        />
        <label className="float-left mb-1">Địa chỉ đến <EnvironmentOutlined/></label>
        <input
          type="text"
          className="form-control form-control-lg mb-3"
          readOnly={true}
          value={location.destination}
        />
        <button type="submit" className="btn btn-block btn-info btn-lg">
          Tìm tài xế
        </button>
      </animated.form>
    </div>
  );
}
const mapStateToProps = state => ({
  closeSideNav: state.app.closeSideNav,
  payloadFCM:state.app.payloadFCM||null,
  bookingCarForm:state.bookingCar,

})

const mapDispatchToProps = {}

const connector = connect(mapStateToProps, mapDispatchToProps);
interface Props extends ConnectedProps<typeof connector> {}


enum StateBooking{
  CREATED,
  CANCELLED,
  REJECTED,
  ACCEPTED
}
 enum RideState {
  STARTED,
  CANCELLED,
  FINISHED
}
const Map = (props:Props) => {
  const {closeSideNav,payloadFCM,bookingCarForm} = props;
  const [payloadFCMValue,setPayloadFCMValue]=useState<Object>(payloadFCM);
  const [viewCoordinate,setViewCoordinate]=useState<coordinate>({
    longitude:bookingCarForm?.departure.coordinate?.longitude as number,
    latitude:bookingCarForm?.departure.coordinate?.latitude as number,
  });
  const [visibleState, setVisibleState] = useState<boolean>(false);
  const [state,setState]=useState(0)
  const [zoom, setZoom] = useState(18)
  const [loadMap, setLoadMap] = useState(false)
  const [lineValue, setLineValue] = useState([] as coordinate)
  const [showPopupDestination, setShowPopupDestination] = useState(false);
  const [showPopupDeparture, setShowPopupDeparture] = useState(false);

  const showDrawerState = () => {
    setVisibleState(true);
  };
  const onCloseDrawer = () => {
    setVisibleState(false);
  };
  const [destinationCoordinate,setDestinationCoordinate]=useState<featuresLocation>({
    value:bookingCarForm.destination.value,
    coordinate:bookingCarForm.destination.coordinate as coordinate
  });
  const [departureCoordinate,setDepartureCoordinate]=useState<featuresLocation>({
    value:bookingCarForm.departure.value,
    coordinate:bookingCarForm.departure.coordinate as coordinate
  });
  const locationBooking:location={
    destination:bookingCarForm.destination.value,
    departure:bookingCarForm.departure.value
  }
  const [driverCoordinate,setDriverCoordinate]=useState<coordinate>({
    longitude:undefined,
    latitude:undefined
  });
  /*const [finishSuccess,setFinishSuccess]=useState<object>();*/
 /* const [driverAccepted,setDriverAccepted]=useState<object>();*/
  useEffect(()=>{

    if(payloadFCM.body.includes(BODYSTATES.DRIVER_ACCEPTED)){

    }
    else if(payloadFCM.body.includes(BODYSTATES.DRIVER_UPDATE_LOCATION)){
      setDriverCoordinate(JSON.parse(payloadFCM.driverLocation) as coordinate)
      console.log(JSON.parse(payloadFCM.driverLocation))
    }
    else if(payloadFCM.body.includes(BODYSTATES.FINISH_SUCCESS)){
      setDriverCoordinate({longitude:undefined,latitude:undefined} as coordinate);
      /*setFinishSuccess(JSON.parse(payloadFCM))*/
    }
    setPayloadFCMValue(payloadFCM);
    console.log(payloadFCMValue)
  },[payloadFCM])


  useEffect(() => {
    const checkDistance = async () => {
      await MapService.getDistance(destinationCoordinate.coordinate as coordinate, departureCoordinate.coordinate as coordinate, accessToken).then((res) => {
        const distance = res.data.routes[0].distance / 1000;
        setLineValue(res.data.routes[0].geometry.coordinates);
      })
    }
    checkDistance();
  }, []);

  useEffect(() => {
    let isMessage=true;
    if(isMessage===true) {
      const notification= MessageLoadMapService.getInstance({loading: 'đang tải map', loaded: 'Tải map thành công!'},loadMap);
    }
    return ()=>{
      isMessage=false;
    }
  }, [loadMap])
  const [viewState, setViewState] = useState({
    longitude: viewCoordinate?.longitude,
    latitude: viewCoordinate?.latitude,
    zoom: zoom,
  });
  const geoJson: GeoJSON.FeatureCollection<any> = {
    type: 'FeatureCollection',
    features: [
      {
        type: 'Feature',
        geometry: {
          type: 'LineString',
          coordinates: lineValue
        },
        properties: {}
      }
    ]
  };
  const mapRef = useRef(null);
  return <MainLayout>
    <ReactMapGL id="map"
                mapboxAccessToken={accessToken}
                mapStyle="mapbox://styles/mapbox/streets-v11"
                style={{
                  width: "80%",
                  height: "600px",
                  borderRadius: "15px",
                  border: `2px solid ${COLOR.MAIN}`,
                  position:'absolute'
                }}
                {...viewState}
                ref={mapRef}

                onClick={(evt) => {

                }}

                onLoad={(map) => {
                  setLoadMap(true)
                }}


                onZoom={evt => {
                }}
                onMove={evt => {
                  setViewState(evt.viewState)
                }}>
      <Source type='geojson' id='source-geojson' data={geoJson}>
        <Layer
          id="lineLayer"
          type="line"
          source="route"
          layout={{
            "line-join": "round",
            "line-cap": "round"
          }}
          paint={{
            "line-color": "red",
            "line-width": 3
          }}
        />
      </Source>

      {showPopupDestination && (
        <Popup
          latitude={destinationCoordinate?.coordinate?.latitude as number}
          longitude={destinationCoordinate?.coordinate?.longitude as number}
          closeButton={true}
          closeOnClick={true}
          onClose={() => setShowPopupDestination(false)}
          anchor="top-right"
        >
          <div>Điểm đến</div>
        </Popup>
      )}

      {showPopupDeparture && (
        <Popup
          latitude={departureCoordinate?.coordinate?.latitude as number}
          longitude={departureCoordinate?.coordinate?.longitude as number}
          closeButton={true}
          closeOnClick={true}
          onClose={() => setShowPopupDeparture(false)}
          anchor="top-right"
        >
          <div>Điểm xuất phát</div>
        </Popup>
      )}

      {
        (driverCoordinate?.latitude!==undefined&&driverCoordinate?.longitude!==undefined)?
        <Marker
          latitude={driverCoordinate?.latitude||undefined}
          longitude={driverCoordinate?.longitude||undefined}>
          <img
            onClick={() => setShowPopupDestination(true)}
            style={{height: 50, width: 50}}
            src="https://xuonginthanhpho.com/wp-content/uploads/2020/03/map-marker-icon.png"
          />
        </Marker>:""
      }

      <Marker
        latitude={destinationCoordinate?.coordinate?.latitude}
        longitude={destinationCoordinate?.coordinate?.longitude}>
        <img
          onClick={() => setShowPopupDestination(true)}
          style={{height: 50, width: 50}}
          src="https://xuonginthanhpho.com/wp-content/uploads/2020/03/map-marker-icon.png"
        />
      </Marker>

      <Marker
        latitude={departureCoordinate?.coordinate?.latitude}
        longitude={departureCoordinate?.coordinate?.longitude}>
        <img
          onClick={() => setShowPopupDeparture(true)}
          style={{height: 50, width: 50}}
          src="https://library.kissclipart.com/20180925/rpe/kissclipart-map-car-icon-clipart-car-google-maps-navigation-c81a6a2d0ecb7a15.png"
        />
      </Marker>
      <GeolocateControl position='top-right'/>
      <NavigationControl position='top-right'/>
      <Menu location={locationBooking} drawer={()=>setVisibleState(prevState => !prevState)}/>
    </ReactMapGL>



    <Drawer
      title="Trạng thái đặt xe"
      placement='bottom'
      closable={false}
      onClose={onCloseDrawer}
      visible={visibleState}
    >
      <p>Some contents...</p>
      <p>Some contents...</p>
      <p>Some contents...</p>
    </Drawer>

  </MainLayout>
}


export default connector(Map)